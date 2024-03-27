<template>
  <v-dialog v-model="dialog" persistent width="1300">
    <v-card>
      <v-card-title>
        <span class="headline">New Assessment</span>
      </v-card-title>
      <v-card-text>
        <v-col cols="12">
          <v-text-field
            v-model="review"
            label="*Review"
            data-cy="reviewInput"
            :rules="[
              (v) =>
                isReviewValid(v) ||
                'Review must be at least 10 characters long',
            ]"
            required
          ></v-text-field>
        </v-col>
      </v-card-text>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn
          color="blue-darken-1"
          variant="text"
          @click="$emit('close-assessment-dialog')"
        >
          Close
        </v-btn>
        <v-btn
          v-if="isReviewValid(review)"
          color="blue-darken-1"
          variant="text"
          @click="createAssessment"
          data-cy="createAssessment"
        >
          Save
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import { Component, Model, Prop, Vue } from 'vue-property-decorator';
import Activity from '@/models/activity/Activity';
import Assessment from '@/models/assessment/Assessment';
import RemoteServices from '@/services/RemoteServices';

@Component
export default class AssessmentDialog extends Vue {
  @Model('dialog', Boolean) dialog!: boolean;
  @Prop({ type: Activity, required: true }) readonly activity!: Activity;

  review: string = '';

  isReviewValid(value: string) {
    return value.length >= 10;
  }

  async createAssessment() {
    if (this.activity.id === null) {
      return;
    }
    try {
      let assessment = new Assessment();
      assessment.review = this.review;
      const result = await RemoteServices.createAssessment(
        assessment,
        this.activity,
      );

      this.$emit('apply', result);
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
  }
}
</script>

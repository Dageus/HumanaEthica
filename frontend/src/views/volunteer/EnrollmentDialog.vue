<template>
  <v-dialog v-model="dialog" persistent width="1300">
    <v-card>
      <v-card-title>
        <span class="headline">Enrollment</span>
      </v-card-title>
      <v-card-text>
        <v-col cols="12">
          <v-text-field
            v-model="motivation"
            label="*Motivation"
            :rules="[isMotivationValid]"
            required
          ></v-text-field>
        </v-col>
      </v-card-text>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn
          color="blue-darken-1"
          variant="text"
          @click="$emit('close-enrollment-dialog')"
        >
          Close
        </v-btn>
        <v-btn
          v-if="isMotivationValidBoolean"
          color="blue-darken-1"
          variant="text"
          @click="createEnrollment"
          data-cy="saveEnrollment"
        >
          Apply
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import { Component, Model, Prop, Vue } from 'vue-property-decorator';
import Enrollment from '@/models/enrollment/Enrollment';
import Activity from '@/models/activity/Activity';
import RemoteServices from '@/services/RemoteServices';

@Component
export default class EnrollmentDialog extends Vue {
  @Model('dialog', Boolean) dialog!: boolean;
  @Prop({ type: Activity, required: true }) readonly activity!: Activity;

  motivation: string = '';

  cypressCondition: boolean = false;

  isMotivationValid(value: string) {
    return value.length >= 10 || 'Motivation must be at least 10 characters.';
  }

  get isMotivationValidBoolean() {
    return typeof this.isMotivationValid(this.motivation) === 'boolean';
  }

  async createEnrollment() {
    if (this.activity.id === null) {
      return;
    }
    try {
      let enrollment = new Enrollment();
      enrollment.motivation = this.motivation;
      console.log(this.activity.id, enrollment);
      const result = await RemoteServices.createEnrollment(
        this.activity.id,
        enrollment,
      );
      this.$emit('apply', result);
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
  }
}
</script>
